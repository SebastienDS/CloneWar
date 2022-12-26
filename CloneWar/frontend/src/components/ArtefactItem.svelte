<script>
  import { link, location } from 'svelte-spa-router';
  import ArtefactDetail from './ArtefactDetail.svelte';

  export let item;


  $: isMyArtefact = $location.endsWith(`/${item.id}`);
  $: activeClass = isMyArtefact ? "active" : "";
</script>

<a href="/artefact/{item.id}" use:link class="has-text-black">
  <div class="{activeClass} bg row has-text-centered">
    <ArtefactDetail {item}/>
  </div>
</a>

<style>
  .bg {
    background-color: #fff;
  }

  .active {
    background-color: deeppink;
  }
</style>