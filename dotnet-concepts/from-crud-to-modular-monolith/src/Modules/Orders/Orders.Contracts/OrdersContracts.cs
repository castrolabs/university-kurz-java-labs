namespace Orders.Contracts;

public sealed record OrderLineRequest(int GameId, int Quantity);

public sealed record PlaceOrderRequest(IReadOnlyList<OrderLineRequest> Lines);

public sealed record OrderLineDto(int GameId, string GameName, int Quantity, decimal UnitPrice);

public sealed record OrderDto(Guid Id, IReadOnlyList<OrderLineDto> Lines, decimal Total);
