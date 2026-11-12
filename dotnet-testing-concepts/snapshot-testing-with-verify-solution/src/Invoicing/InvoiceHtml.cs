using System.Globalization;
using System.Net;
using System.Text;

namespace Invoicing;

public static class InvoiceHtml
{
    public static string Render(Invoice invoice)
    {
        var html = new StringBuilder();
        var c = CultureInfo.InvariantCulture;
        html.AppendLine(string.Create(c, $"<h1>Invoice {invoice.Number}</h1>"));
        html.AppendLine(string.Create(c, $"<p>Issued {invoice.IssuedAt:yyyy-MM-dd HH:mm:ss} UTC to {WebUtility.HtmlEncode(invoice.Customer)}</p>"));
        html.AppendLine("<table>");
        foreach (var line in invoice.Lines)
        {
            html.AppendLine(string.Create(c, $"  <tr><td>{WebUtility.HtmlEncode(line.Product)}</td><td>{line.Quantity}</td><td>{line.UnitPrice:0.00}</td><td>{line.Total:0.00}</td></tr>"));
        }

        html.AppendLine("</table>");
        html.AppendLine(string.Create(c, $"<p>Subtotal {invoice.Subtotal:0.00}, tax {invoice.Tax:0.00}, total {invoice.Total:0.00}</p>"));
        if (invoice.Note is not null)
        {
            html.AppendLine($"<p class=\"note\">{WebUtility.HtmlEncode(invoice.Note)}</p>");
        }

        return html.ToString();
    }
}
