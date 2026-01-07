# carf-stubs

This is the stubs repository for the Crypto Asset Reporting Framework (CARF) team's registration journey

## Scenarios

### Register with ID API Stub

We use the idNumber and idType to determine the stub response.

idNumber comes from the answer entered in the user journey on either the UTR or NINO entry pages

idType what value is that they have entered: either "UTR" or "NINO"


The different responses are:

500 - Start UTR / NINO with "9" or "Y" respectively

404 - Start UTR / NINO with "8" or "X" respectively

200 (with all optional fields empty) - Start UTR / NINO with "7" or "W" respectively

200 (non uk response, Org affinity group only) - Start UTR with "6"

200 (with all optional fields full) - Start UTR / NINO with anything else ("1" or "A" suggested respectively)

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").