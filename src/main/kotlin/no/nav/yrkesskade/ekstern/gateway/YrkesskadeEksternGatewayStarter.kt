package no.nav.yrkesskade.ekstern.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class YrkesskadeEksternGatewayStarter

fun main(args: Array<String>) {
    runApplication<YrkesskadeEksternGatewayStarter>(*args)
}