# Let's Encrypt with cert-manager: HTTP-01 vs DNS-01 (Solution)

## Overview

The Certificate lists `shop.lab` and `www.shop.lab`, and the HTTP-01 solver creates its challenge Ingresses with `ingressClassName: traefik`. Two tests wait for the certificate and check what both hosts serve.

## Key Concepts

- **HTTP-01 proves one host name at a time.** The CA fetches `http://<name>/.well-known/acme-challenge/<token>` on port 80, so every name needs public DNS pointing at your ingress and port 80 open. It cannot prove a wildcard: for `*.shop.lab` the CA offers only DNS-01, and with an HTTP-01-only issuer cert-manager reports `no configured challenge solvers can be used for this challenge`, without creating any Challenge.
- **DNS-01 proves control of the zone.** A TXT record at `_acme-challenge.<name>` works for wildcards and for hosts that are not reachable from the internet, at the price of giving cert-manager credentials that can edit DNS.
- **The solver has to be served.** cert-manager creates a temporary Ingress per challenge with the class in the solver; a class no controller serves gives `wrong status code '404'` in cert-manager's self check.
- **Self check first.** Before telling the CA, cert-manager fetches the token itself through the cluster DNS. Split-horizon DNS (a cluster that cannot resolve its own public names) keeps challenges pending with `no such host` even when the CA could reach you.
- **Debug top-down.** Certificate, then CertificateRequest, Order, Challenge: the lowest object that is not done holds the reason. Let's Encrypt's rate limits (duplicate certificates, failed validations) make testing against its staging server, or Pebble, worthwhile.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/lets-encrypt-http01-vs-dns01-solution test
```
