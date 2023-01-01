<script lang="ts">
  import ArtefactItem from "./ArtefactItem.svelte";


  let items = [];

  const a = fetch("/api/artefacts")
    .then(res => res.json())
    .then(json => {
      console.log(json)
      items = json
    })
    .catch(error => console.error(error))

</script>

<div class="has-background-info rows">
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