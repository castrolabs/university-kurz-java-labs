using System.Diagnostics;
using System.Diagnostics.Metrics;

namespace LlmCostLab.Tests;

/// <summary>Listens to one ActivitySource and one Meter by name, no OpenTelemetry SDK needed.</summary>
public sealed class TelemetryProbe : IDisposable
{
    private readonly ActivityListener _activityListener;
    private readonly MeterListener _meterListener = new();

    public List<Activity> Activities { get; } = [];

    public List<(string Instrument, long Value, string? TokenType)> Measurements { get; } = [];

    public TelemetryProbe(string sourceName)
    {
        _activityListener = new ActivityListener
        {
            ShouldListenTo = s => s.Name == sourceName,
            Sample = (ref ActivityCreationOptions<ActivityContext> _) => ActivitySamplingResult.AllDataAndRecorded,
            ActivityStopped = a => { lock (Activities) { Activities.Add(a); } },
        };
        ActivitySource.AddActivityListener(_activityListener);

        _meterListener.InstrumentPublished = (instrument, listener) =>
        {
            if (instrument.Meter.Name == sourceName)
            {
                listener.EnableMeasurementEvents(instrument);
            }
        };
        _meterListener.SetMeasurementEventCallback<int>((i, v, tags, _) => Add(i.Name, v, tags));
        _meterListener.SetMeasurementEventCallback<long>((i, v, tags, _) => Add(i.Name, v, tags));
        _meterListener.SetMeasurementEventCallback<double>((i, v, tags, _) => Add(i.Name, (long)v, tags));
        _meterListener.Start();
    }

    private void Add(string instrument, long value, ReadOnlySpan<KeyValuePair<string, object?>> tags)
    {
        string? tokenType = null;
        foreach (var tag in tags)
        {
            if (tag.Key == "gen_ai.token.type")
            {
                tokenType = tag.Value?.ToString();
            }
        }

        lock (Measurements) { Measurements.Add((instrument, value, tokenType)); }
    }

    public void Dispose()
    {
        _meterListener.Dispose();
        _activityListener.Dispose();
    }
}
