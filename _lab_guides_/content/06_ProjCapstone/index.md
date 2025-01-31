## Project Capstone (bonus)

> 💡 **Hint:** If you got lost in the previous section executing the command below will take care of everything
```
docker compose down && git reset --hard && git checkout logs-finished
```
<br />
<hr/>

*Capstone (noun) [ˈkap-ˌstōn]*
- the high point
- crowning achievement

### Why this section?

Your Demo Application has one remaining secret to reveal. And we've kept it for the end of that topic for those students who want to get challenged today.

We named this ***Project Capstone*** because after completing this section, you can congratulate yourself for being able to grasp the core concepts of OpenTelemetry and what is required to implement it in your environment.

This section is a **bonus** section and is optional. You can always revisit this section of the lab anytime.

### What is Project Capstone about?
- Helps you to apply the core concepts of OpenTelemetry traces, metrics and logs
- It can be broken down into 3 sections, and each section builds up your knoweldge on the following topics:
  - Auto instrumentation of a Python service to receive baseline observability signals.
  - Implement Trace Context Propagation to link traces between Java and Python service.
  - Tests your knowledge on where to capture data for added visibility.

There is only a dependency on the first section, meaning that you need to be able to receive a baseline of observability signals before you attempt section 2 and 3. However, section 2 and 3 are mutually exclusive of each other, meaning that you can attempt section 3 without completing section 2 and vice versa. 

### Scenario
- A team has been formed to create a service to produce quotes.
- The team decided on using Python Flask to build the business logic.
- This services will be called by the Java application and is flexible to extended anytime.
- For now, the Java Backend service will be the only service making a http call to the python service. 
