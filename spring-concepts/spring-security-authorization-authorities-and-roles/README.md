# Spring Security Authorization: Authorities And Roles

## Goal

Understand the difference between `hasAuthority()` and `hasRole()` — and the `ROLE_` prefix rule
that makes the two easy to mix up.

## Prerequisites

- Basic Spring Framework knowledge
- Familiarity with `UserDetails`/`GrantedAuthority` (see the `spring-security-user-management` lab)

## Task

`hasRole("ADMIN")` internally checks for the authority string `"ROLE_ADMIN"` — it silently
*prepends* `"ROLE_"` before comparing. `hasAuthority("ADMIN")` checks for the literal string
`"ADMIN"`, with no prefixing at all. Granting a user the authority `"ADMIN"` and then checking
authorization with `hasRole("ADMIN")` silently denies access that should have been granted, because
`hasRole()` is really looking for `"ROLE_ADMIN"`.

Implement two classes:

- `AuthorityGrantor` — builds `UserDetails` carrying different flavors of the "admin" privilege
- `AccessChecks` — runs a role-based check and an authority-based check against a user's granted
  authorities, using `AuthorityAuthorizationManager`

## Instructions

Complete the following TODOs:

- TODO-00: `AuthorityGrantor.grantAdminRole()` — grant the correctly-prefixed `"ROLE_ADMIN"`
- TODO-01: `AuthorityGrantor.grantAdminAuthorityWithoutRolePrefix()` — grant the literal `"ADMIN"`
- TODO-02: `AccessChecks.isGrantedByRole()` — check authorization with `AuthorityAuthorizationManager.hasRole(...)`
- TODO-03: `AccessChecks.isGrantedByAuthority()` — check authorization with `AuthorityAuthorizationManager.hasAuthority(...)`

Run the tests until they all pass. Pay close attention to which combinations of grant/check pass and
which don't — that asymmetry is the whole point of this lab.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-security-authorization-authorities-and-roles test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-security-authorization-authorities-and-roles
mvn test
```

## Bonus

- TODO-04 (optional): `AuthorityGrantor.grantAdminRoleUsingRolesBuilder()` — reach the same
  `"ROLE_ADMIN"` outcome using the `User` builder's `.roles("ADMIN")` method instead of
  `.authorities("ROLE_ADMIN")`
