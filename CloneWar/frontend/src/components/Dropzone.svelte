<script>
    import { createEventDispatcher } from "svelte";


  let files = null;

  const dispatch = createEventDispatcher();

  const submitFile = () => {
    dispatch("filenameChanged", {
      filename: files[0].name
    });
  }
</script>

<div class="file has-name is-boxed" class:is-success="{files !== null}">
  <label class="file-label">
    <input class="file-input" type="file" name="resume" bind:files on:change={submitFile}/>
    <span class="file-cta">
      <span class="file-icon">
        <i class="fas fa-upload" />
      </span>
      <span class="file-label"> Choose a file… </span>
    </span>
    {#if files}
      <span class="file-name"> {files[0].name} </span>
    {/if}
  </label>
</div>
