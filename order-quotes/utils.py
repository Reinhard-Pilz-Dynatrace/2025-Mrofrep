import logging
from datetime import datetime
from opentelemetry import trace
from opentelemetry.trace.status import Status, StatusCode

tracer = trace.get_tracer_provider().get_tracer(__name__)

"""Returns the n-th fibonacci number."""
def fibonacci(n: int) -> int:
    if n <= 1:
        return n
    
    if n >= 20:
        raise Exception("fibonacci number too large")
    
    n2, n1 = 0, 1
    for _ in range(2, n):
        n2, n1 = n1, n1+n2
    
    return n2 + n1

"""Attempts to calculate Fibonacci value at the given number"""
def process(n: int) -> int:
    with tracer.start_as_current_span("process") as span:
        try:
            f = fibonacci(n)
            return f
        except Exception as e:
            logging.getLogger().exception("SeedNumber=%s",n)
            span.record_exception(e)
            span.set_status(Status(StatusCode.ERROR))