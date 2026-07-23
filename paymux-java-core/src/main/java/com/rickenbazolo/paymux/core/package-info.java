/**
 * Core abstractions and interfaces for the Paymux Java SDK.
 * <p>
 * This package provides the fundamental building blocks for Mobile Money integrations:
 * <ul>
 *   <li>{@link com.rickenbazolo.paymux.core.MobileMoneyClient} - Base client interface</li>
 *   <li>{@link com.rickenbazolo.paymux.core.MobileMoneyConfig} - Configuration interface</li>
 *   <li>{@link com.rickenbazolo.paymux.core.http} - HTTP abstraction layer</li>
 *   <li>{@link com.rickenbazolo.paymux.core.operations} - Common operation interfaces</li>
 * </ul>
 * <p>
 * Operator-specific modules (for example {@code paymux-java-mtn-congo} and future provider modules)
 * implement these interfaces to provide concrete implementations for each Mobile Money provider.
 *
 * @author Ricken Bazolo
 * @since 0.1.0
 */
package com.rickenbazolo.paymux.core;
