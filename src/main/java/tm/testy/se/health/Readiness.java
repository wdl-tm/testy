package tm.testy.se.health;

import tm.testy.se.misc.Paths;
import io.helidon.config.Config;
import io.helidon.webserver.Service;

final class Readiness {

  private Readiness () {}

  static Service service (
    final Config config
  ) {

    return
      rules ->
        rules.get (
          Paths._READINESS,
          ( in, out ) ->
            out.send ()
        );

  }

}
