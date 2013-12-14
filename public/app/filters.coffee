app = angular.module 'app.filters', []

app.filter 'capitalize', ->
  (input, scope) ->
    val = input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase()
    val.replace('_', ' ')