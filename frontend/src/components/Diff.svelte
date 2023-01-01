<script>
  import DiffContent from "./DiffContent.svelte";
  
  export let params;

  let diff = [];

  fetch(`/api/diff/${params.referenceId}/${params.cloneId}`)
    .then(res => res.json())
    .then(json => {
      console.log(json)
      diff = json
    })
    .catch(error => console.error(error))
</script>


<div class="container has-background-grey full-height scrollable">
  {#each diff as content}
    <div class="columns">
      <div class="column full-height">
        <DiffContent file={content.reference} lines={content.diffs.map(d => d.reference)}/>
      </div>
      <div class="column full-height">
        <DiffContent file={content.file} lines={content.diffs.map(d => d.file)}/>
      </div>
    </div>
  {/each}
  

</div>