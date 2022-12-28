<script>
  import ArtefactDetail from "./ArtefactDetail.svelte";

  export let params;

  $: id = params.id;

  let showMore = false;
  let reference = {
    id: 0,
    name: ""
  };

  $: allClones = [];

  const fetchItems = (id) => {
    fetch("/api/clones?id=" + id)
    .then(res => res.json())
    .then(json => {
      console.log(json)
      reference = json.reference;
      allClones = json.clones;
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
    {#each items as item}
      <div class="row radius">
        <div class="is-flex is-justify-content-space-around">
          <div class="is-flex is-justify-content-center is-align-items-center width">
            <ArtefactDetail item={item.artefact}/>
          </div>
          <div class="is-flex is-justify-content-center is-align-items-center width">
            {item.percentage} %
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

  .width {
    width: 250px;
  }
</style>