package com.kurz.customconfigprops;

import org.springframework.context.annotation.Configuration;

// TODO-03: Annotate this class with @EnableConfigurationProperties(MailProperties.class) so
// Spring registers MailProperties as a bean bound from the environment. Without this, no
// MailProperties bean exists in the context at all.
@Configuration
public class MailConfig {
}
