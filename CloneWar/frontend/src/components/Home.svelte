<script>
  import Dropzone from "./Dropzone.svelte";

  let mainJar = null;
  let sourceJar = null;

  const setMainJar = (event) => mainJar = event.detail.filename;
  const setSourceJar = (event) => sourceJar = event.detail.filename;

  $: canSubmit = mainJar !== null && sourceJar != null;

  const submit = () => {
    const dataArray = new FormData();
    dataArray.append("mainJar", mainJar);
    dataArray.append("sourceJar", sourceJar);

    fetch("/api/jars", {
      method: "POST",
      headers: {
        "Content-Type": "multipart/form-data"
      },
      body: dataArray
    })
    .then(res => console.log(res.json()))
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
