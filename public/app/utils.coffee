app = angular.module 'app.utils', []

app.factory 'Utils', ->
    indexById: (iterable, checkId) ->
        index = -1
        angular.forEach iterable, (obj, i) ->
          console.log
          if obj.id is checkId
            index = i
        index
    fromId: (iterable, checkId) ->
        the_obj = {}
        angular.forEach iterable, (obj) ->
          if obj.id is checkId
            the_obj = obj
        the_obj
    chunkIt: (iterable, cols) ->
      i for i in [0..iterable.length] by cols