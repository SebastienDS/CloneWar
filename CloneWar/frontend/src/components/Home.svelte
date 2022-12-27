<script>
  import Router, { link } from "svelte-spa-router";


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
  <div class="column is-one-fifth has-background-primary full-height">
    <a href="/" use:link class="has-text-black">
      <div class="h is-flex is-justify-content-center is-align-items-center is-flex-direction-column has-background-grey-lighter">
        <span class="icon is-size-3 height">
          <i class="fas fa-home"></i>
        </span>
      </div>
    </a>

    <div class="max-height scrollable">
      <ArtefactList/>
    </div>
  </div>
  <div class="column is-dark has-background-info is-flex is-justify-content-center is-align-items-center">
    <Router {routes}/>
  </div>
</div>

<style>
  .height {
    height: 50px;
  }

  .max-height {
    height: calc(100% - 50px);
  }
</style>