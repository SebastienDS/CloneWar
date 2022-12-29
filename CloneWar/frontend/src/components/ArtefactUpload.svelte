<script>
  import Dropzone from "./Dropzone.svelte";

  let mainJar = null;
  let sourceJar = null;

  const setMainJar = (event) => mainJar = event.detail.file;
  const setSourceJar = (event) => sourceJar = event.detail.file;

  $: canSubmit = mainJar !== null && sourceJar != null;

  let showLoader = false;

  const submit = () => {
    const formData = new FormData();
    formData.append(mainJar, mainJar, mainJar.name);
    formData.append(sourceJar, sourceJar, sourceJar.name);

    console.log(mainJar, sourceJar);

    const options = {
      method: "POST",
      body: formData
    }

    showLoader = true;
    fetch("/api/analyze", options)
      .then(res => res.json())
      .then(json => {
        console.log(json);
        showLoader = false;
        location.reload();
      })
      .catch(error => console.error(error))
  }
</script>

<div class="is-flex is-flex-direction-column is-align-items-center">
  <div class="is-flex">
    <div class="m-3">
      <Dropzone on:fileChanged={setMainJar}/>
    </div>
    <div class="m-3">
      <Dropzone on:fileChanged={setSourceJar}/>
    </div>
  </div>
  
  <div class="m-3">
    {#if showLoader}
      <div class="loader is-size-3"></div>
    {/if}
    {#if !showLoader}
      <button type="submit" class="button is-primary" on:click={submit} disabled="{canSubmit === false}">Submit</button>
    {/if}
  </div>
</div>