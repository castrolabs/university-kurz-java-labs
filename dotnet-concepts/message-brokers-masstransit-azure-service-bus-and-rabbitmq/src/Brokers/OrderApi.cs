using MassTransit;

namespace Brokers;

public sealed class OrderApi(IPublishEndpoint publish, ISendEndpointProvider send)
{
    // Event: one publish, zero or many subscribers.
    public async Task SubmitAsync(Guid orderId, decimal total, Guid? messageId = null, CancellationToken ct = default)
    {
        // TODO-01: publish an OrderSubmitted. When messageId has a value, assign it to ctx.MessageId (a client retry
        // carries the same id). Use the Publish overload that takes an Action<PublishContext<T>>.
        await Task.CompletedTask;
        throw new NotImplementedException("Not implemented yet.");
    }

    // Command: one sender, one queue, one consumer.
    public async Task RequestShipmentAsync(Guid orderId, CancellationToken ct = default)
    {
        // TODO-02: send a ShipOrder to the queue "queue:" + Topology.ShippingQueue: GetSendEndpoint(new Uri(...)),
        // then Send. A command goes to one queue and one consumer, unlike a published event.
        await Task.CompletedTask;
        throw new NotImplementedException("Not implemented yet.");
    }
}

public static class Topology
{
    public const string ShippingQueue = "shipping";
}
