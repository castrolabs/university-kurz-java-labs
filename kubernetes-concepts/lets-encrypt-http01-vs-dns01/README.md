# Let's Encrypt with cert-manager: HTTP-01 vs DNS-01

## Goal

Get certificates from an ACME CA (the protocol of Let's Encrypt) with cert-manager and the HTTP-01 challenge, and learn what HTTP-01 can and cannot prove. The tests run cert-manager, Traefik and Pebble (Let's Encrypt's own ACME test server, which validates challenges for real) on a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container, which downloads cert-manager v1.21.2 and pulls Pebble from `ghcr.io`)
- The article "Let's Encrypt with cert-manager: HTTP-01 vs DNS-01"
- The lab "cert-manager: Issuers and a Private CA" helps, but is not required

## Task

The test makes `shop.lab` and `www.shop.lab` resolve to Traefik in the cluster DNS (as public DNS would), runs Pebble at `https://pebble:14000/dir` (`src/test/resources/k8s/`, given), and applies your `src/main/resources/k8s/tls.yaml`: an ACME ClusterIssuer, a Certificate and an Ingress. It checks that:

- `certificate/shop` becomes Ready and covers `shop.lab` and `www.shop.lab`;
- both hosts serve that certificate over HTTPS (issued by Pebble's CA).

You only edit `tls.yaml`. `server` and `caBundle` point at Pebble; with Let's Encrypt only `server` would change.

## Instructions

Run the tests first. The failure message shows the Certificate, CertificateRequest, Order and Challenges, and the latest warnings: the ACME objects are where issuance problems are explained.

- TODO-00: The Certificate asks for `*.shop.lab`. Find out why no challenge is even created, and ask for what HTTP-01 can prove.
- TODO-01: The challenges stay pending. Fix the solver.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/lets-encrypt-http01-vs-dns01 test
```

## Bonus (Optional)

- TODO-02 (optional): A wildcard needs DNS-01. Write (without running) the solver for your DNS provider in cert-manager's documentation. Which credentials does it need, and why is that a bigger risk than HTTP-01?
