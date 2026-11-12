using Microsoft.AspNetCore.Mvc;
using Microsoft.FeatureManagement.Mvc;

namespace FlagsLab;

[ApiController]
[Route("reports")]
[FeatureGate(Flags.Reports)]
public class ReportsController : ControllerBase
{
    [HttpGet]
    public string Get() => "reports";
}
