<script>
  import { link } from 'svelte-spa-router';
  import ArtefactDetail from "./ArtefactDetail.svelte";

  export let params;

  $: id = params.id;

  let showMore = false;
  let showLoader = false;
  let reference = {
    id: 0,
    name: ""
  };

  $: allClones = [];

  const fetchItems = (id) => {
    showLoader = true;
    showMore = false;
    allClones = [];

    fetch("/api/clones/" + id)
    .then(res => res.json())
    .then(json => {
      console.log(json)
      reference = json.reference;
      allClones = json.clones;
      showLoader = false;
    })
    .catch(error => console.error(error))
    return true;
  }

  $: updated = fetchItems(id);
  $: items = showMore ? allClones : allClones.slice(0, 5);
</script>


<div class="container has-background-grey full-height scrollable rows">
  <div class="has-background-grey-lighter row space">
    <ArtefactDetail item={reference}/>
  </div>

  <div class="row rows has-background-grey-light">
    {#if showLoader}
      <div class="row is-flex is-justify-content-center has-background-grey">
        <div class="loader is-size-3 m-3"></div>
      </div>
    {/if}
    
    {#each items as item}
      <a href="/diff/{reference.id}/{item.artefact.id}" use:link class="has-text-black">
        <div class="row separator">
          <div class="is-flex is-justify-content-space-around">
            <div class="is-flex is-justify-content-center is-align-items-center width">
              <ArtefactDetail item={item.artefact}/>
            </div>
            <div class="is-flex is-justify-content-center is-align-items-center width">
              {item.percentage} %
            </div>
          </div>
        </div>
      </a>
    {/each}
  </div>

  <div class="row has-text-centered has-background-grey space">
    {#if !showMore && allClones.length > items.length}
      <button on:click={e => showMore = true}>Afficher tout</button>
    {/if}
  </div>
</div>

<style>
  .separator {
    border-bottom: 1px solid rgb(145, 142, 142);
  }

  .space {
    padding: 25px;
  }

  .width {
    width: 250px;
  }
</style>