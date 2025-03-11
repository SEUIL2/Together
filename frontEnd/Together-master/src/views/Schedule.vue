<template>
  <div id="spread-host">
    <gc-spread-sheets hostClass="spreadHost" @workbookInitialized="initSpread">
    </gc-spread-sheets>
  </div>
</template>

<script>
import "@grapecity/spread-sheets/styles/gc.spread.sheets.excel2016colorful.css";

export default {
  name: "app",
  components: {},
  data: function () {
    return {};
  },
  methods: {
    initSpread: function (spread) {
      this.spread = spread;
    },
  },
};
</script>

<style>
.spreadHost {
  width: 100%;
  height: 600px;
}
</style>