Blueprints are created in 3 steps. We use a test clock that we advance ourselves, and start it at `2001-01-01`.

* At time 1 `2001-01-01` we create an Imgur Image entry.
* At time 2 `2001-01-02` we upload the blueprint string and get back a sha.
* At time 3 `2001-01-03` we upload the blueprint post.

We start our deep dive on this third step.

# POST Request Body

We create a blueprint post by `POST`ing to `/api/blueprint/`.

:include-file: non-destructive-updates1.json {title: "POST /api/blueprint/", commentsType: "inline"}

# POST Response Body

The response includes all the properties we sent, highlighted below, plus server-generated information.

:include-file: non-destructive-updates2.json {title: "POST /api/blueprint/ response", highlight: ["title", "sha", "imgurId", "descriptionMarkdown", "category", "name"]}

# Temporal Response

Let's look at the response again, labeling some of the temporal features that will be covered in upcoming sections.

:include-file: non-destructive-updates3.json {title: "POST /api/blueprint/ response", commentsType: "inline"}

# Non-destructive updates

Next, we update the blueprint by `PATCH`ing `/api/blueprint/{id}?version=1`.

:include-json: non-destructive-updates4.json {title: "PATCH /api/blueprint/{id}?version=1"}

# Response

The response includes the updated properties we sent, plus our first temporal updates.

The edits are reflected at time 4: `2001-01-04`.

:include-file: non-destructive-updates5.diff {title: "PATCH /api/blueprint/{id}?version=1 response"}

# As-of query

In the next section, we'll perform our first as-of query to prove to ourselves that no data has been lost.
