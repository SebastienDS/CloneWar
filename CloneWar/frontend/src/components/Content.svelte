<script>
  import ArtefactDetail from "./ArtefactDetail.svelte";

  export let params;

  let showMore = false;

  $: details = {
    id: params.id,
    name: `Artefact ${params.id}`,
  }

  const getItems = (id) => {
    const items = [];
    showMore = false;
    for (let index = id; index < 50; index++) {
      items.push({
        name: "Artefact " + index,
        id: index
      });
    }
    return items;
  }

  $: allItems = getItems(params.id);
  $: items = showMore ? allItems : allItems.slice(0, 5);
</script>


<div class="container has-background-grey full-height scrollable rows">
  <div class="has-background-grey-lighter row space">
    <ArtefactDetail item={details}/>
  </div>


  <div class="row rows has-background-grey-light">
    {#each items as item}
      <div class="row radius">
        <div class="is-flex is-justify-content-space-around">
          <ArtefactDetail {item}/>
          <div class="is-flex is-justify-content-center is-align-items-center">
            100 %
          </div>
        </div>
      </div>
    {/each}
  </div>

  {#if !showMore}
    <div class="row has-text-centered has-background-grey space"><button on:click={e => showMore = true}>Afficher tout</button></div>
  {/if}
</div>

<style>
  .radius {
    border-bottom: 1px solid rgb(145, 142, 142);
  }

  .space {
    padding: 25px;
  }
</style>