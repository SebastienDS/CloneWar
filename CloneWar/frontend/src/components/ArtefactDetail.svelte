<script>
  export let item;
  $: date = new Date(item.insertionDate).toLocaleString();
</script>

<div class="is-flex is-justify-content-center is-align-items-center is-flex-direction-column">
  <div class="">
    <div class="">{item.name}</div>
  </div>
  <div class="rows">
    <div class="row has-text-centered">url</div>
    <div class="row has-text-centered">{date}</div>
    <!-- <div class="row has-text-centered">analyze en cours ?</div> -->
  </div>
</div>