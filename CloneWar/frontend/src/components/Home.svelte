<script>
  import Dropzone from "./Dropzone.svelte";

  let mainJar = null;
  let sourceJar = null;

  const setMainJar = (event) => mainJar = event.detail.filename;
  const setSourceJar = (event) => sourceJar = event.detail.filename;

  $: canSubmit = mainJar !== null && sourceJar != null;

  const submit = () => {
    const formData = new FormData();
    formData.append(mainJar, mainJar);
    formData.append(sourceJar, sourceJar);

    const options = {
      method: "POST",
      body: formData
    }

    fetch("/api/jars", options)
    .then(res => console.log(res))
    .catch(error => console.log(error))
  }
</script>

<div
  class="container full-height is-flex is-justify-content-center is-align-items-center"
>
  <div class="is-flex is-flex-direction-column is-align-items-center">
    <div class="is-flex">
      <div class="m-3">
        <Dropzone on:filenameChanged={setMainJar}/>
      </div>
      <div class="m-3">
        <Dropzone on:filenameChanged={setSourceJar}/>
      </div>
    </div>
    
    <div class="m-3">
      <button type="submit" class="button is-primary" on:click={submit} disabled="{canSubmit === false}">Submit</button>
    </div>
  </div>
</div>
