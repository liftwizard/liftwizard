Blueprints are created in 3 steps, starting with a test clock set at `2001-01-01`.

1. At time 1 (`2001-01-01`), we create an Imgur Image entry.
2. At time 2 (`2001-01-02`), we upload the blueprint string and receive a sha.
3. At time 3 (`2001-01-03`), we upload the blueprint post.

In this documentation, we'll focus on the third step.

# POST Request Body

We create a blueprint post by `POST`ing to `/api/blueprint/`.

:include-file: non-destructive-updates1.json5 {title: "POST /api/blueprint/", commentsType: "inline"}

# POST Response Body

The response includes all the properties we sent, along with server-generated information.

:include-file: non-destructive-updates2.json {title: "POST /api/blueprint/ response", highlight: ["title", "sha", "imgurId", "descriptionMarkdown", "category", "name"]}

# Temporal Response

Here's the same response, with some temporal features labeled. These will be covered in upcoming sections.

:include-file: non-destructive-updates3.json5 {title: "POST /api/blueprint/ response", commentsType: "inline"}

# Non-destructive updates

Next, we update the blueprint by `PATCH`ing `/api/blueprint/{id}?version=1`.

:include-json: non-destructive-updates4.json {title: "PATCH /api/blueprint/{id}?version=1"}

# Response

The response includes the updated properties we sent, plus our first temporal updates.

The edits are reflected at time 4 (`2001-01-04`).

:include-file: non-destructive-updates5.diff {title: "PATCH /api/blueprint/{id}?version=1 response"}

# As-of query

In the next section, we'll perform our first as-of query to prove to ourselves that no data has been lost.
