<script>
  import ArtefactItem from "./ArtefactItem.svelte";

  const items = [];
  for (let index = 1; index < 50; index++) {
    items.push("Artefact " + index);
  }
</script>

<div class="has-background-info rows scrollable">
  {#each items as item}
    <div class="padding margin">
      <ArtefactItem name={item}/>
    </div>
	{/each}
</div>

<style scoped>
  .padding {
    padding: 15px 5px;
  }

  .scrollable {
    height: 100%;
    overflow-y: auto;
  }
</style>