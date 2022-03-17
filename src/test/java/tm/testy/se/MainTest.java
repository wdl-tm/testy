
package tm.testy.se;


import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webserver.SocketConfiguration;
import io.helidon.webserver.WebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tm.testy.se.misc.Paths;
import tm.testy.se.misc.Strings;
import tm.testy.se.server.Bootstrap;

import javax.json.*;
import java.util.function.IntUnaryOperator;
import java.util.function.ToDoubleFunction;

import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainTest {

  private static final JsonBuilderFactory JSON  = Json.createBuilderFactory ( emptyMap () );
  public static final  JsonObject         EMPTY = JSON.createObjectBuilder ().build ();

  private static WebServer server;
  private static WebClient calc;
  private static WebClient health;
  private static WebClient metrics;

  @BeforeAll
  public static void setup () {

    server =
      Bootstrap.server ()
        .await ();

    final var prefix =
      "http://localhost:";

    calc =
      WebClient
        .builder ()
        .baseUri ( prefix + server.port () )
        .addMediaSupport ( JsonpSupport.create () )
        .build ();

    health =
      WebClient
        .builder ()
        .baseUri ( prefix + server.configuration ().namedSocket ( Strings.HEALTH ).map ( SocketConfiguration::port ).orElse ( server.port () ) )
        .addMediaSupport ( JsonpSupport.create () )
        .build ();

    metrics =
      WebClient
        .builder ()
        .baseUri ( prefix + server.configuration ().namedSocket ( Strings.METRICS ).map ( SocketConfiguration::port ).orElse ( server.port () ) )
        .addMediaSupport ( JsonpSupport.create () )
        .build ();

  }


  @AfterAll
  public static void shutdown ()
  throws Exception {

    if ( server != null ) {

      server.shutdown ()
        .toCompletableFuture ()
        .get ( 10, SECONDS );

    }

  }


  private JsonArray toJson (
    final double... doubles
  ) {

    final var builder =
      JSON.createArrayBuilder ();

    for ( final var d : doubles ) {
      builder.add ( d );
    }

    return
      builder.build ();

  }

  private JsonObject toJson (
    final String label,
    final int value
  ) {

    final var builder =
      JSON.createObjectBuilder ();

    return
      builder.add (
        label,
        value
      ).build ();

  }


  @Test
  void testAdd () {

    assertEquals (
      30D,
      calc.get ()
        .path ( "/add" )
        .submit ( toJson ( 10, 20 ), JsonObject.class )
        .map ( js -> js.getJsonNumber ( "result" ) )
        .map ( JsonNumber::doubleValue )
        .await ()
    );

  }


  @Test
  void testSubtract () {

    assertEquals (
      10D,
      calc.get ()
        .path ( "/subtract" )
        .submit ( toJson ( 20, 10 ), JsonObject.class )
        .map ( js -> js.getJsonNumber ( "result" ) )
        .map ( JsonNumber::doubleValue )
        .await ()
    );

  }


  @Test
  void testDivision () {

    final ToDoubleFunction< double[] > test =
      args ->
        calc.get ()
          .path ( "/division" )
          .submit ( toJson ( args ), JsonObject.class )
          .map ( js -> js.getJsonNumber ( "result" ) )
          .map ( JsonNumber::doubleValue )
          .await ();

    assertEquals (
      30D / 10D,
      test.applyAsDouble (
        new double[]{30D, 10D}
      )
    );

    assertEquals (
      10D / 30D,
      test.applyAsDouble (
        new double[]{10D, 30D}
      )
    );

    assertEquals (
      0D,
      test.applyAsDouble (
        new double[]{30D, 0D}
      )
    );

  }

  @Test
  void testRandom () {

    final IntUnaryOperator test =
      count ->
        calc.get ()
          .path ( "/random" )
          .submit ( toJson ( "count", count ), JsonObject.class )
          .map ( js -> js.getJsonArray ( "result" ) )
          .await ()
          .size ();


    assertEquals (
      0,
      test.applyAsInt (
        -1
      )
    );

    assertEquals (
      0,
      test.applyAsInt (
        0
      )
    );


    assertEquals (
      12,
      test.applyAsInt (
        12
      )
    );

    assertEquals (
      10,
      test.applyAsInt (
        10
      )
    );

    assertEquals (
      1024,
      test.applyAsInt (
        2048
      )
    );


    assertEquals (
      10,
      calc.get ()
        .path ( "/random" )
        .submit ( EMPTY, JsonObject.class )
        .map ( json -> json.getJsonArray ( "result" ) )
        .await ()
        .size ()
    );

  }

  
  @Test
  void testMetrics () {

    assertEquals (
      200,
      metrics.get ()
        .path ( Paths._METRICS )
        .request ()
        .await ()
        .status ()
        .code ()
    );

  }

  @Test
  void testHealth () {

    assertEquals (
      200,
      health.get ()
        .path ( Paths._LIVENESS )
        .request ()
        .await ()
        .status ()
        .code ()
    );

    assertEquals (
      200,
      health.get ()
        .path ( Paths._READINESS )
        .request ()
        .await ()
        .status ()
        .code ()
    );

  }
}
