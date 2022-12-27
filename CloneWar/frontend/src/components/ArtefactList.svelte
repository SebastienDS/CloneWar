<script lang="ts">
  import ArtefactItem from "./ArtefactItem.svelte";


  const items = [];
  for (let index = 1; index < 50; index++) {
    items.push({
      name: "Artefact " + index,
      id: index
    });
  }

</script>

<div class="has-background-info rows scrollable">
  {#each items as item}
    <div class="padding margin">
      <ArtefactItem {item}/>
    </div>
	{/each}
</div>

<style>
  .padding {
    padding: 5px 10px;
  }

  .padding:first-child {
    padding-top: 10px;
  }

  .padding:last-child {
    padding-bottom: 10px;
  }
</style>