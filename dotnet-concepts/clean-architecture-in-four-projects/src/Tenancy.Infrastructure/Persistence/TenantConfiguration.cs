using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Tenancy.Domain;

namespace Tenancy.Infrastructure.Persistence;

internal sealed class TenantConfiguration : IEntityTypeConfiguration<Tenant>
{
    public void Configure(EntityTypeBuilder<Tenant> builder)
    {
        // TODO-04: Map the Tenant: Id is the key, Subdomain is required with a maximum length
        //   of 63, and the database itself must reject two tenants with the same subdomain.
        //   The mapping lives here so the Domain stays free of persistence concerns.
    }
}
