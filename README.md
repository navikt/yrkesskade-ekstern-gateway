# yrkesskade-ekstern-gateway
En proxy-tjeneste som tilgjengeliggjør yrkesskade-tjenester for internett. Tar imot kall fra eksterne tjenester, veksler inn Maskinporten-token til TokenX, og redirecter til andre yrkesskade-tjenester.

# Generelt
Behovet for denne tjenesten kommer av at det er ønskelig å sende yrkesskademeldinger direkte fra et kvalitetssystem til NAVs nye innmeldingsløsning.
Denne tjenesten tilgjengeliggjør de aktuelle APIene som trenger å nås utenfra, og autentiserer innkommende trafikk ved å kreve gyldig Maskinporten fra en kjent klient.

# Tjenester det redirectes til

| Tjeneste                    | Path                           | Scope                             |
|-----------------------------|--------------------------------|-----------------------------------|
| Yrkesskademelding API       | /api/v1/skademeldinger         | nav:yrkesskade:skademelding.write |
| Yrkesskademelding API-doc   | /api/v1/skademeldinger/api-doc | N/A                               |
| Yrkesskade kodeverk         | /api/v1/kodeverk/**            | nav:yrkesskade:kodeverk.read      |
| Yrkesskade kodeverk API-doc | /api/v1/kodeverk/api-doc       | N/A                               |

# Miljøer
| miljø | URL                                                   |
|-------|-------------------------------------------------------|
| DEV   | https://yrkesskade-ekstern-gateway.ekstern.dev.nav.no |
| PROD  | https://yrkesskade-ekstern-gateway.nav.no             |

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan:
- stilles som issues her på GitHub
- stilles til yrkesskade@nav.no

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #team-yrkesskade.