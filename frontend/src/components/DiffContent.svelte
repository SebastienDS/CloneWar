<script>
  export let file;
  export let lines;

  const removeDuplicates = array => {
    const res = [array[0]];
    for (let index = 1; index < array.length; index++) {
      const element = array[index];
      if (res.at(-1).line != element.line) {
        res.push(element);
      }
    }
    return res;
  }
</script>

<div>
  <div class="is-flex is-justify-content-center m-3 has-text-black">{file}</div>

  {#each removeDuplicates(lines) as item}
    <pre class="line-height"><span>{item.line}</span>  <span class="font-size">{item.content}</span></pre>
  {/each}
</div>

<style>
  .line-height {
    line-height: 1rem;
    padding: 5px;
  }

  .font-size {
    font-size: 0.5rem;
  }
</style>