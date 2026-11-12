namespace CompiledModelLab;

// 60 small entities, each pointing at the previous one, to make the model big enough to measure.

public sealed class Entity01
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
}

public sealed class Entity02
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity01? Previous { get; set; }
}

public sealed class Entity03
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity02? Previous { get; set; }
}

public sealed class Entity04
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity03? Previous { get; set; }
}

public sealed class Entity05
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity04? Previous { get; set; }
}

public sealed class Entity06
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity05? Previous { get; set; }
}

public sealed class Entity07
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity06? Previous { get; set; }
}

public sealed class Entity08
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity07? Previous { get; set; }
}

public sealed class Entity09
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity08? Previous { get; set; }
}

public sealed class Entity10
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity09? Previous { get; set; }
}

public sealed class Entity11
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity10? Previous { get; set; }
}

public sealed class Entity12
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity11? Previous { get; set; }
}

public sealed class Entity13
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity12? Previous { get; set; }
}

public sealed class Entity14
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity13? Previous { get; set; }
}

public sealed class Entity15
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity14? Previous { get; set; }
}

public sealed class Entity16
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity15? Previous { get; set; }
}

public sealed class Entity17
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity16? Previous { get; set; }
}

public sealed class Entity18
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity17? Previous { get; set; }
}

public sealed class Entity19
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity18? Previous { get; set; }
}

public sealed class Entity20
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity19? Previous { get; set; }
}

public sealed class Entity21
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity20? Previous { get; set; }
}

public sealed class Entity22
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity21? Previous { get; set; }
}

public sealed class Entity23
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity22? Previous { get; set; }
}

public sealed class Entity24
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity23? Previous { get; set; }
}

public sealed class Entity25
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity24? Previous { get; set; }
}

public sealed class Entity26
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity25? Previous { get; set; }
}

public sealed class Entity27
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity26? Previous { get; set; }
}

public sealed class Entity28
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity27? Previous { get; set; }
}

public sealed class Entity29
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity28? Previous { get; set; }
}

public sealed class Entity30
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity29? Previous { get; set; }
}

public sealed class Entity31
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity30? Previous { get; set; }
}

public sealed class Entity32
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity31? Previous { get; set; }
}

public sealed class Entity33
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity32? Previous { get; set; }
}

public sealed class Entity34
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity33? Previous { get; set; }
}

public sealed class Entity35
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity34? Previous { get; set; }
}

public sealed class Entity36
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity35? Previous { get; set; }
}

public sealed class Entity37
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity36? Previous { get; set; }
}

public sealed class Entity38
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity37? Previous { get; set; }
}

public sealed class Entity39
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity38? Previous { get; set; }
}

public sealed class Entity40
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity39? Previous { get; set; }
}

public sealed class Entity41
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity40? Previous { get; set; }
}

public sealed class Entity42
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity41? Previous { get; set; }
}

public sealed class Entity43
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity42? Previous { get; set; }
}

public sealed class Entity44
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity43? Previous { get; set; }
}

public sealed class Entity45
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity44? Previous { get; set; }
}

public sealed class Entity46
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity45? Previous { get; set; }
}

public sealed class Entity47
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity46? Previous { get; set; }
}

public sealed class Entity48
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity47? Previous { get; set; }
}

public sealed class Entity49
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity48? Previous { get; set; }
}

public sealed class Entity50
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity49? Previous { get; set; }
}

public sealed class Entity51
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity50? Previous { get; set; }
}

public sealed class Entity52
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity51? Previous { get; set; }
}

public sealed class Entity53
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity52? Previous { get; set; }
}

public sealed class Entity54
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity53? Previous { get; set; }
}

public sealed class Entity55
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity54? Previous { get; set; }
}

public sealed class Entity56
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity55? Previous { get; set; }
}

public sealed class Entity57
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity56? Previous { get; set; }
}

public sealed class Entity58
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity57? Previous { get; set; }
}

public sealed class Entity59
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity58? Previous { get; set; }
}

public sealed class Entity60
{
    public int Id { get; set; }
    public string Name { get; set; } = "";
    public decimal Amount { get; set; }
    public DateTime CreatedAt { get; set; }
    public int? PreviousId { get; set; }
    public Entity59? Previous { get; set; }
}
