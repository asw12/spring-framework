package org.springframework.web.reactive.result.method.annotation;

import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.testfixture.http.server.reactive.bootstrap.HttpServer;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelSendOperatorRaceConditionIntegrationTest extends AbstractRequestMappingIntegrationTests {

	@Override
	protected ApplicationContext initApplicationContext() {
		return new AnnotationConfigApplicationContext(WebConfig.class, TestRestController.class);
	}


	@ParameterizedHttpServerTest
	void handlePublishOn(HttpServer httpServer) throws Exception {
		startServer(httpServer);

		String expected = "Hello world!";
		assertThat(performGet("/test", new HttpHeaders(), String.class).getBody()).isEqualTo(expected);
	}

	@EnableWebFlux
	static class WebConfig {
	}
	@RestController
	@SuppressWarnings("unused")
	private static class TestRestController {

		@GetMapping("/test")
		public Publisher<String> handlerWithPublishOn() {
			return Flux.just("Hello ", "world", "!")
					.publishOn(Schedulers.boundedElastic());
		}
	}
}
