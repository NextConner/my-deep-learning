<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script setup>
const url = ref('http://doc.jtcool.vip/jtcool-vue')

function goto() {
  window.open(url.value)
}
</script>