using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Tenancy.Domain;

namespace Tenancy.Infrastructure.Persistence;

internal sealed class TenantConfiguration : IEntityTypeConfiguration<Tenant>
{
    public void Configure(EntityTypeBuilder<Tenant> builder)
    {
        builder.HasKey(t => t.Id);
        builder.Property(t => t.Subdomain).HasMaxLength(63).IsRequired();
        builder.HasIndex(t => t.Subdomain).IsUnique();
    }
}
