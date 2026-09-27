# cert-manager: Issuers and a Private CA (Solution)

## Overview

A namespaced `Issuer` of type `ca` in `web` uses the `lab-ca` Secret; the Certificate asks for `shop.lab` from that Issuer and writes `shop-tls`, which is the Secret the Ingress references. Two tests check the Certificate and a verified HTTPS request.

## Key Concepts

- **Issuer vs ClusterIssuer.** An `Issuer` works in its own namespace and reads Secrets from it. A `ClusterIssuer` works for every namespace and reads Secrets from cert-manager's "cluster resource namespace" (`cert-manager` by default), so with the CA in `web` it reports `Error getting keypair for CA issuer: secrets "lab-ca" not found`. Put a shared CA in `cert-manager` for a ClusterIssuer, or use an Issuer next to the Secret.
- **The chain of names.** Certificate `secretName` is the Secret cert-manager writes; the Ingress `tls.secretName` is the Secret the controller reads. A mismatch fails silently: the controller serves its default certificate (`CN=TRAEFIK DEFAULT CERT`).
- **Controllers choose certificates by name.** Traefik matches the requested host (SNI) against the names in each certificate. A certificate for `www.shop.lab` is valid, issued and referenced, and still never used for `shop.lab`. List every name clients use in `dnsNames`.
- **Read the served certificate, not just the objects.** `curl -v` (or `openssl s_client -servername`) shows the subject and issuer actually presented, which settles in one step whether the problem is trust, names, or the wrong certificate.
- **A private CA must be distributed.** Every client needs the CA certificate in its trust store (here, `--cacert`); in Java that means a truststore, in containers a mounted bundle.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/cert-manager-issuers-and-private-ca-solution test
```
