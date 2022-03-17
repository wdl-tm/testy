package tm.testy.se.server;


import tm.testy.se.health.Health;
import tm.testy.se.metrics.Metrics;
import tm.testy.se.service.Calc;
import io.helidon.common.LogConfig;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.webserver.SocketConfiguration;
import io.helidon.webserver.WebServer;
import tm.testy.se.misc.Strings;

public final class Bootstrap {

  private Bootstrap () {}

  public static Single< WebServer > server () {

    LogConfig
      .configureRuntime ();

    final var config =
      Config.create ();

    final var server =
      WebServer
        .builder ( Calc.routing ( config ) )
        .config ( config.get ( Strings.SERVER ) )
        .addMediaSupport ( JsonpSupport.create () )
        .addSocket ( socket ( config, Strings.HEALTH ) )
        .addNamedRouting ( Strings.HEALTH, Health.routing ( config ) )
        .addSocket ( socket ( config, Strings.METRICS ) )
        .addNamedRouting ( Strings.METRICS, Metrics.routing ( config ) )
        .build ();

    return
      server.start ();

  }

  private static SocketConfiguration socket (
    final Config config,
    final String name
  ) {

    return
      SocketConfiguration
        .builder ()
        .config (
          config
            .get ( Strings.SOCKETS )
            .get ( name ) )
        .build ();

  }

}

