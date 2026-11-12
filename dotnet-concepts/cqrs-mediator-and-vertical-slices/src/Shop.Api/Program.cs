using Shop.Api.Features.Orders;
using Shop.Api.Mediator;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSingleton<OrderStore>();
builder.Services.AddScoped<IUnitOfWork, FakeUnitOfWork>();
builder.Services.AddMediator(typeof(Program).Assembly);
builder.Services.AddSingleton<IValidator<PlaceOrder>, PlaceOrderValidator>();

// Registration order is execution order: logging wraps validation, which wraps the transaction.
builder.Services.AddScoped(typeof(IPipelineBehavior<,>), typeof(LoggingBehavior<,>));
builder.Services.AddScoped(typeof(IPipelineBehavior<,>), typeof(ValidationBehavior<,>));
builder.Services.AddScoped(typeof(IPipelineBehavior<,>), typeof(TransactionBehavior<,>));

var app = builder.Build();

app.MapOrdersEndpoints();

app.Run();

public partial class Program;
