package tm.testy.se.metrics;

import io.helidon.config.Config;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webserver.Routing;

/**
 * Serves up a {@code "/metrics"} service endpoint.
 */

public final class Metrics {

  private Metrics () {}

  public static Routing routing (
    final Config config
  ) {

    return
      Routing
        .builder ()
        .register (
          MetricsSupport.create (
            config
          )
        ).build ();

  }

}
