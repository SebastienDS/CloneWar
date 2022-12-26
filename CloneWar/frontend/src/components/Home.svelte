<script>
  import Router from "svelte-spa-router";


  import ArtefactList from "./ArtefactList.svelte";
  import ArtefactUpload from "./ArtefactUpload.svelte";
  import Content from "./Content.svelte";
  import GoBack from "./GoBack.svelte";

  
  const routes = {
    "/": ArtefactUpload,
    "/artefact/:id": Content,
    "*": GoBack
  }
</script>

<div class="columns full-height no-MP">
  <div class="column is-one-fifth has-background-primary">
    <ArtefactList/>
  </div>
  <div class="column is-dark has-background-info is-flex is-justify-content-center is-align-items-center">
    <Router {routes}/>
  </div>
</div>