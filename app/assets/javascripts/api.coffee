'use strict'

api = angular.module 'app.factories', ['restangular']

api.factory 'Api', ['Restangular', (Restangular) ->
  Restangular.withConfig (RestangularConfigurer) ->
    RestangularConfigurer.setBaseUrl('/api')
]

api.factory 'SubService', ->
  cache = {}

  publish: (topic, args) ->
    cache[topic] and angular.forEach cache[topic], (func) ->
      func.apply null, args or []
  subscribe: (topic, callback) ->
    cache[topic] = [] if not cache[topic]
    cache[topic].push callback
    [topic, callback]
  unsubscribe: (handle) ->
    t = handle[0]
    cache[t] and angular.forEach cache[t] (val, idx) ->
      cache[t].splice idx, 1 if val is handle[1]

api.factory 'DudeService', ->
  dudes = []

  dudes: dudes
  add: (newDude) ->
    dudes.push newDude
    dudes
  remove: (remDude) ->
    angular.forEach dudes, (dude, idx) ->
      if remDude.id is dude.id
        dudes.splice(idx, 1)
    dudes