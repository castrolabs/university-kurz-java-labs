using MassTransit;

namespace Brokers;

public sealed class OrderApi(IPublishEndpoint publish, ISendEndpointProvider send)
{
    // Event: one publish, zero or many subscribers.
    public async Task SubmitAsync(Guid orderId, decimal total, Guid? messageId = null, CancellationToken ct = default)
    {
        await publish.Publish(new OrderSubmitted(orderId, total), ctx =>
        {
            if (messageId is { } id) ctx.MessageId = id;
        }, ct);
    }

    // Command: one sender, one queue, one consumer.
    public async Task RequestShipmentAsync(Guid orderId, CancellationToken ct = default)
    {
        var endpoint = await send.GetSendEndpoint(new Uri("queue:" + Topology.ShippingQueue));
        await endpoint.Send(new ShipOrder(orderId), ct);
    }
}

public static class Topology
{
    public const string ShippingQueue = "shipping";
}
