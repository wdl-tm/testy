
package tm.testy.se.service;

import io.helidon.config.Config;
import io.helidon.webserver.Handler;
import io.helidon.webserver.Routing;
import io.helidon.webserver.Service;

import javax.json.*;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.DoubleBinaryOperator;

import static io.helidon.webserver.Handler.create;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Optional.of;
import static java.util.stream.DoubleStream.generate;

public final class Calc
  implements Service {

  private static final Random RANDOM = new Random ();

  private static final JsonBuilderFactory JSON =
    Json.createBuilderFactory ( Map.of () );

  public static final String RESULT = "result";

  private static final RuntimeException WRONG_NUMBER_OF_ARGUMENTS_EXCEPTION =
    new RuntimeException ( "wrong number of arguments" );

  private static final RuntimeException CALCULATION_ERROR_EXCEPTION =
    new RuntimeException ( "calculation error" );

  private static final BiConsumer< JsonArrayBuilder, JsonArrayBuilder > JSON_ARRAY_COMBINER =
    ( left, right ) -> {
    };

  private static final DoubleBinaryOperator ADD      = ( left, right ) -> left + right;
  private static final DoubleBinaryOperator DIVIDE   = ( left, right ) -> right != 0D ? left / right : 0D;
  private static final DoubleBinaryOperator SUBTRACT = ( left, right ) -> left - right;

  private Calc () {}

  public static Routing routing (
    final Config config
  ) {

    return
      Routing
        .builder ()
        .register ( new Calc () )
        .build ();

  }


  /**
   * A service registers itself by updating the routing rules.
   *
   * @param rules the routing rules.
   */

  @Override
  public void update (
    final Routing.Rules rules
  ) {

    rules
      .get (
        "/add",
        add ()
      ).get (
        "/subtract",
        subtract ()
      ).get (
        "/division",
        divide ()
      ).get (
        "/random",
        random ()
      );

  }


  private static Handler handler (
    final DoubleBinaryOperator operator
  ) {

    return
      Handler.create (
        JsonArray.class,
        ( in, out, args ) ->
          out.send (
            JSON.createObjectBuilder ()
              .add (
                RESULT,
                of ( args )
                  .filter ( array -> array.size () == 2 )
                  .map ( Collection::stream )
                  .orElseThrow ( () -> WRONG_NUMBER_OF_ARGUMENTS_EXCEPTION )
                  .map ( JsonValue::toString )
                  .mapToDouble ( Double::parseDouble )
                  .reduce ( operator )
                  .orElseThrow ( () -> CALCULATION_ERROR_EXCEPTION )
              ).build ()
          )
      );
  }


  private Handler add () {

    return
      handler (
        ADD
      );

  }

  private Handler subtract () {

    return
      handler (
        SUBTRACT
      );

  }

  private Handler divide (
  ) {

    return
      handler (
        DIVIDE
      );

  }

  private Handler random (
  ) {

    return
      create (
        JsonObject.class,
        ( in, out, arg ) ->
          out.send (
            JSON.createObjectBuilder ()
              .add (
                RESULT,
                generate ( RANDOM::nextDouble )
                  .limit ( limit ( arg ) )
                  .collect (
                    JSON::createArrayBuilder,
                    JsonArrayBuilder::add,
                    JSON_ARRAY_COMBINER
                  )
              ).build ()
          )
      );

  }

  private int limit (
    final JsonObject arg
  ) {

    return
      min (
        max (
          arg.getInt (
            "count",
            10
          ),
          0 ),
        1024
      );

  }

}