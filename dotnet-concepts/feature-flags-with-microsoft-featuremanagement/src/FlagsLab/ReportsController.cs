using Microsoft.AspNetCore.Mvc;

namespace FlagsLab;

// TODO-06: Gate this controller with the Reports flag ([FeatureGate]); it must answer 404 while the flag is off.
[ApiController]
[Route("reports")]
public class ReportsController : ControllerBase
{
    [HttpGet]
    public string Get() => "reports";
}
