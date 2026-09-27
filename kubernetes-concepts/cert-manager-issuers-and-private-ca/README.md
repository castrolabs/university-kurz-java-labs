# cert-manager: Issuers and a Private CA

## Goal

Issue a certificate from the company's private CA with cert-manager and serve it through the ingress controller, so that a client that trusts only that CA can open `https://shop.lab`. The tests install cert-manager on a real Kubernetes cluster (k3s with its bundled Traefik, started by Testcontainers) and verify the TLS connection with `curl --cacert`.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container, which downloads cert-manager v1.21.2 and pulls `busybox` and `curl`)
- The article "cert-manager: Issuers and a Private CA"

## Task

The test installs cert-manager, then deploys (`src/test/resources/k8s/apps.yaml`, given) the `shop` app in `web`, the private CA as the Secret `lab-ca` **in `web`**, and a `client` Pod whose `curl` trusts only that CA. Then it applies your `src/main/resources/k8s/tls.yaml` (an issuer, a Certificate and an Ingress) and checks that:

- `certificate/shop` becomes Ready;
- `curl --cacert /ca/ca.crt https://shop.lab/` succeeds and returns the shop's page.

You only edit `tls.yaml`, and the CA Secret stays where it is.

## Instructions

Run the tests first. The failure messages show the issuers' and the Certificate's conditions, and the subject and issuer of the certificate Traefik actually served.

- TODO-00: Make the issuer Ready.
- TODO-01: Make Traefik serve the issued certificate.
- TODO-02: Make that certificate valid for `shop.lab`.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/cert-manager-issuers-and-private-ca test
```

## Bonus (Optional)

- TODO-03 (optional): Instead of a Certificate object, add the annotation `cert-manager.io/issuer: lab-ca` to the Ingress and delete the Certificate. What does cert-manager create, and from which fields of the Ingress?
