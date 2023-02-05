First, we create a blueprint by `POST`ing to `/api/blueprint/`.

:include-json: non-destructive-updates1.json {title: "POST /api/blueprint/"}

# Response

The response includes all the properties we sent, plus server-generated information.

:include-file: non-destructive-updates2.json {title: "POST /api/blueprint/ response", highlight: ["title", "sha", "imgurId", "descriptionMarkdown", "category", "name"]}

# Preview

Let's look at the response again, labeling some of the temporal features that will be covered in upcoming sections.

:include-file: non-destructive-updates3.json {title: "POST /api/blueprint/ response", commentsType: "inline"}

# Non-destructive updates

Next, we update the blueprint by `PATCH`ing to `/api/blueprint/{id}`.

:include-json: non-destructive-updates4.json {title: "PATCH /api/blueprint/{id}?version=1"}
