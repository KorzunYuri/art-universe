/**
 * Utilities for W3C Trace Context generation
 *
 * Implements W3C Trace Context specification for distributed tracing
 * https://www.w3.org/TR/trace-context/
 */

/**
 * Generates a random hex string of specified length
 */
function randomHex(length: number): string {
  const bytes = new Uint8Array(length / 2);
  crypto.getRandomValues(bytes);
  return Array.from(bytes)
    .map(b => b.toString(16).padStart(2, '0'))
    .join('');
}

/**
 * Generates a W3C Trace Context trace-id (128-bit / 32 hex characters)
 */
export function generateTraceId(): string {
  return randomHex(32);
}

/**
 * Generates a W3C Trace Context span-id (64-bit / 16 hex characters)
 */
export function generateSpanId(): string {
  return randomHex(16);
}

/**
 * Creates a W3C Trace Context traceparent header value
 *
 * Format: version-trace-id-span-id-trace-flags
 * - version: always "00"
 * - trace-id: 32 hex characters (128 bits)
 * - span-id: 16 hex characters (64 bits)
 * - trace-flags: "01" for sampled, "00" for not sampled
 *
 * @param sampled Whether this trace should be sampled (default: true)
 * @returns A valid traceparent header value
 */
export function createTraceparent(sampled: boolean = true): string {
  const version = '00';
  const traceId = generateTraceId();
  const spanId = generateSpanId();
  const traceFlags = sampled ? '01' : '00';

  return `${version}-${traceId}-${spanId}-${traceFlags}`;
}
