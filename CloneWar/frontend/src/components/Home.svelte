<script>
  import ArtefactList from "./ArtefactList.svelte";
  import ArtefactUpload from "./ArtefactUpload.svelte";
</script>

<div class="columns full-height no-MP">
  <div class="column is-one-quarter has-background-primary">
    <ArtefactList/>
  </div>
  <div class="column is-dark has-background-info is-flex is-justify-content-center is-align-items-center">
    <ArtefactUpload/>
  </div>
</div>